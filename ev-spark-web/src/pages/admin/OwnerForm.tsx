import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { Button } from "@/components/ui/button";
import {
  Form,
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from "@/components/ui/form";
import { Input } from "@/components/ui/input";

// Interface for Owner
interface Owner {
  id: string;
  name: string;
  email: string;
  phone: string;
  registeredDate: string;
  vehicles: number;
  active: boolean;
}

const ownerFormSchema = z.object({
  name: z.string().min(2, { message: "Name must be at least 2 characters" }),
  email: z.string().email({ message: "Please enter a valid email address" }),
  phone: z.string().min(10, { message: "Please enter a valid phone number" }),
  vehicles: z.coerce.number().int().min(0).default(0),
});

type OwnerFormValues = z.infer<typeof ownerFormSchema>;

interface OwnerFormProps {
  owner: Owner | null;
  onSubmit: (data: Partial<Owner>) => void;
  onCancel: () => void;
}

export function OwnerForm({ owner, onSubmit, onCancel }: OwnerFormProps) {
  // Set default values based on whether we're editing or creating
  const defaultValues = owner
    ? {
        name: owner.name,
        email: owner.email,
        phone: owner.phone,
        vehicles: owner.vehicles,
      }
    : {
        name: "",
        email: "",
        phone: "",
        vehicles: 0,
      };

  const form = useForm({
    resolver: zodResolver(ownerFormSchema),
    defaultValues,
  }) as ReturnType<typeof useForm<OwnerFormValues>>;

  const handleSubmit = (values: OwnerFormValues) => {
    onSubmit(values);
  };

  return (
    <Form {...form}>
      <form onSubmit={form.handleSubmit(handleSubmit)} className="space-y-4">
        <FormField
          control={form.control}
          name="name"
          render={({ field }) => (
            <FormItem>
              <FormLabel>Full Name</FormLabel>
              <FormControl>
                <Input placeholder="Full name" {...field} />
              </FormControl>
              <FormMessage />
            </FormItem>
          )}
        />

        <FormField
          control={form.control}
          name="email"
          render={({ field }) => (
            <FormItem>
              <FormLabel>Email</FormLabel>
              <FormControl>
                <Input type="email" placeholder="Email address" {...field} />
              </FormControl>
              <FormMessage />
            </FormItem>
          )}
        />

        <FormField
          control={form.control}
          name="phone"
          render={({ field }) => (
            <FormItem>
              <FormLabel>Phone Number</FormLabel>
              <FormControl>
                <Input placeholder="Phone number" {...field} />
              </FormControl>
              <FormMessage />
            </FormItem>
          )}
        />

        <FormField
          control={form.control}
          name="vehicles"
          render={({ field }) => (
            <FormItem>
              <FormLabel>Number of Vehicles</FormLabel>
              <FormControl>
                <Input type="number" placeholder="0" min="0" {...field} />
              </FormControl>
              <FormMessage />
            </FormItem>
          )}
        />

        <div className="flex justify-end space-x-2 pt-4">
          <Button type="button" variant="outline" onClick={onCancel}>
            Cancel
          </Button>
          <Button type="submit">
            {owner ? "Update Owner" : "Create Owner"}
          </Button>
        </div>
      </form>
    </Form>
  );
}
